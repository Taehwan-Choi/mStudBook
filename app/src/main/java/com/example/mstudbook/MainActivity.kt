package com.example.mstudbook

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.GlobalScope
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {

    private lateinit var searchTextInput: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter



    private val dataList = mutableListOf<Pair<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views by ID
        searchTextInput = findViewById(R.id.search_text_input)
        searchButton = findViewById(R.id.search_button)
        recyclerView = findViewById(R.id.search_result_recycler_view)




        // Set up RecyclerView
        adapter = MyAdapter(this, dataList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Set button click listener
        searchButton.setOnClickListener {


            onSearchButtonClick()
            
            
//            키보드 보이지 않게 숨겨주는 코드
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.applicationWindowToken, 0)




        }
    }

    private fun onSearchButtonClick() {
        // Get input text
        val query = searchTextInput.text.toString()

        // Check if query is not empty
        if (!TextUtils.isEmpty(query)) {
            // Add the query to the list and notify adapter



            if ( isValidDigit(query) != "0") {

//웹페이지 오픈
                val webPage: Uri = Uri.parse("https://studbook.kra.co.kr/html/info/ind/s_majuck.jsp?mabun=${isValidDigit(query)}")
                val intent = Intent(Intent.ACTION_VIEW, webPage)
                startActivity(intent)

//                여기서, 해당 정보를 찾지 못했을 경우 경고 알림을 띄우고 자동으로 액티비티가 종료되며 이전 액티비티로 돌아가도록 기능 만들어야 함

//실제로는 말 정보를 보여주는 액티비티를 열어줘야 함
//                val intent = Intent(getActivity(), HorseInfo::class.java)
//                intent.putExtra("horse_ID", isValidDigit(query))
//                startActivity(intent)

            } else if(isValidText(query)){    // 마명 검색일 경우 로직
                //            마명 검색시의 로직

                dataList.clear()
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }




                searchWeb(query)


                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }


                // Clear input field
//            searchTextInput.text.clear()


            }else{ //적절한 마명, 마번 양식이 아닐 경우 경고창

                showAlertDialog("올바른 마번 및 마명을 입력하세요.")

            }



        }
    }

    class MyAdapter(
        private val context: Context,  // Context 추가
        private val dataList: List<Pair<String, String>>
    ) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        class ViewHolder(view: View, val button: Button) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val button = Button(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 25f
            }

            val layout = LinearLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                addView(button)
            }

            return ViewHolder(layout, button)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (text, horseId) = dataList[position]
            holder.button.text = text
            holder.button.setOnClickListener {

                val webPage: Uri = Uri.parse("https://studbook.kra.co.kr/html/info/ind/s_majuck.jsp?mabun=$horseId")
                val intent = Intent(Intent.ACTION_VIEW, webPage)
                holder.itemView.context.startActivity(intent)

//            실제로는 액티비티를 열어야 함
//            val intent = Intent(holder.itemView.context, HorseInfo::class.java)
//            intent.putExtra("horse_ID", horseId)
//            holder.itemView.context.startActivity(intent)
            }

            if (dataList.size == 1 && position == 0) {
                holder.button.performClick()
            }


        }

        override fun getItemCount() = dataList.size

    }


    private fun isValidDigit(input: String): String {
        val regex = Regex("^\\d{5}$|^\\d{7}$")

        return if (regex.matches(input)) {
            if (input.length == 5) {
                "00$input"
            } else {
                input
            }
        } else {
            "0"
        }
    }

    private fun isValidText(input: String): Boolean {
        val regex = Regex("^[가-힣a-zA-Z ]+$")
        return regex.matches(input)
    }


    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }



    private fun searchWeb(query: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val encodedSearchText = URLEncoder.encode(query, "euc-kr")
            val url = "https://studbook.kra.co.kr/search.jsp?gsearchtxt=$encodedSearchText&gsearchopt=1"
//                https://studbook.kra.co.kr/search.jsp?gsearchtxt=%B8%DE%B4%CF&gsearchopt=1       메니
//                https://studbook.kra.co.kr/search.jsp?gsearchtxt=%C6%C4%BF%F6%BA%ED%B7%B9%C0%CC%B5%E5&gsearchopt=1    파워블레이드
//                https://studbook.kra.co.kr/search.jsp?gsearchtxt=%BE%F8%B4%C2%B8%B6%B8%ED&gsearchopt=1             없는마명

            val doc = Jsoup.parse(fetchWebPage(url))

            try {

                val tbody = doc.select("#divList > table > tbody")
                val trElements = tbody.select("tr")


                for (trElement in trElements) {
                    val elements = trElement.select("td")
                    if (elements.size >= 2) {
                        println(elements[0].text() + elements[1].text()+ "$$$$$")
                        val info: Pair<String, String>

                        if(elements[0].text().endsWith("자마")){
                            info  = Pair(elements[0].text()+ " (" + elements[4].text().takeLast(4) +")", elements[1].text())
                        } else{
                            info  = Pair(elements[0].text(), elements[1].text())
                        }


                        dataList.add(info)

                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    if (dataList.isEmpty()) {
                        showAlertDialog("검색 결과가 없습니다.")
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }



            }catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun fetchWebPage(url : String): String {
        try {
            val html = url
            val userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
            val doc = Jsoup.connect(html).userAgent(userAgent).get()
            return doc.toString()
            //        parse는 로컬 html을 파싱하는 것이고, 인터넷에서 정보를 가져오는 것은 connect를 해야 함
            //        val doc: Document = Jsoup.parse(html)
            //        println(doc)

        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }





}