package com.example.mstudbook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLEncoder

class HorseSearchFragment : Fragment() {

    private lateinit var searchTextInput: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter

    private val dataList = mutableListOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horse_search, container, false)

        // Find views by ID
        searchTextInput = view.findViewById(R.id.search_text_input)
        searchButton = view.findViewById(R.id.search_button)
        recyclerView = view.findViewById(R.id.search_result_recycler_view)

        // Set up RecyclerView
        adapter = MyAdapter(requireContext(), dataList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set button click listener
        searchButton.setOnClickListener {
            onSearchButtonClick()

            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        return view


    }

    private fun onSearchButtonClick() {
        // Get input text
        val query = searchTextInput.text.toString()

        // Check if query is not empty
        if (!TextUtils.isEmpty(query)) {
            // Add the query to the list and notify adapter

            if (isValidDigit(query) != "0") {
                // Open webpage
                val webPage: Uri = Uri.parse("https://studbook.kra.co.kr/html/info/ind/s_majuck.jsp?mabun=${isValidDigit(query)}")
                val intent = Intent(Intent.ACTION_VIEW, webPage)
                startActivity(intent)

                // Show alert dialog if information not found and go back to previous activity
                // Open horse information activity if found
                // val intent = Intent(requireActivity(), HorseInfo::class.java)
                // intent.putExtra("horse_ID", isValidDigit(query))
                // startActivity(intent)
            } else if (isValidText(query)) {
                // Clear previous search results
                dataList.clear()
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }

                // Search web
                searchWeb(query)

                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }

                // Clear input field
                // searchTextInput.text.clear()
            } else {
                // Show alert dialog for invalid input
                showAlertDialog("올바른 마번 및 마명을 입력하세요.")
            }
        }
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
        AlertDialog.Builder(requireContext())
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

            val doc = Jsoup.parse(fetchWebPage(url))

            try {
                val tbody = doc.select("#divList > table > tbody")
                val trElements = tbody.select("tr")

                for (trElement in trElements) {
                    val elements = trElement.select("td")
                    if (elements.size >= 2) {
                        println(elements[0].text() + elements[1].text() + "$$$$$")
                        val info: Pair<String, String>

                        if (elements[0].text().endsWith("자마")) {
                            info = Pair(elements[0].text() + " (" + elements[4].text().takeLast(4) + ")", elements[1].text())
                        } else {
                            info = Pair(elements[0].text(), elements[1].text())
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

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWebPage(url: String): String {
        return try {
            val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
            val doc = Jsoup.connect(url).userAgent(userAgent).get()
            doc.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    class MyAdapter(
        private val context: Context,
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
            }

            if (dataList.size == 1 && position == 0) {
                holder.button.performClick()
            }
        }

        override fun getItemCount() = dataList.size
    }



}