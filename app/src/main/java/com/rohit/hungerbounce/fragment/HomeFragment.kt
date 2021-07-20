package com.rohit.hungerbounce.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.adapter.HomeRecyclerAdapter
import com.rohit.hungerbounce.model.Restaurants
import com.rohit.hungerbounce.utility.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var recyclerHome:RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
 /*  val restaurantList= arrayListOf("Pind Tadka","Heera Mahal")
    val restaurantsInfoList= arrayListOf<Restaurants>(
        Restaurants(1,"Pind Tadka","4.5","299",R.drawable.hunger),
                Restaurants(2,"Heera Mahal","4.5","399",R.drawable.hunger2)

    )*/
 val restaurantsInfoList= arrayListOf<Restaurants>()
    lateinit var recyclerAdapter: HomeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_home, container, false)
        recyclerHome=view.findViewById(R.id.recyclerViewHome)
        layoutManager=LinearLayoutManager(activity)
        progressLayout=view.findViewById(R.id.progressLayout)
        progressBar=view.findViewById(R.id.progressBar)
        progressLayout.visibility=View.VISIBLE


        val queue= Volley.newRequestQueue(activity as Context)
        val url="http://13.235.250.119/v2/restaurants/fetch_result/ "
        if(ConnectionManager().checkConnectivity(activity as Context)){
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET,url,null,
            Response.Listener<JSONObject> {
                response ->
            progressLayout.visibility=View.GONE
           try{
               val data = response.getJSONObject("data")
               val success = data.getBoolean("success")
               if (success) {

                   val resArray = data.getJSONArray("data")
                   for (i in 0 until resArray.length()) {
                       val resObject = resArray.getJSONObject(i)
                       val restaurant = Restaurants(
                           resObject.getString("id").toInt(),
                           resObject.getString("name"),
                           resObject.getString("rating"),
                           resObject.getString("cost_for_one").toInt(),
                           resObject.getString("image_url")
                       )
                    restaurantsInfoList.add(restaurant)
                    recyclerAdapter= HomeRecyclerAdapter(restaurantsInfoList,activity as Context)
                    recyclerHome.adapter=recyclerAdapter
                    recyclerHome.layoutManager=layoutManager
                    recyclerHome.addItemDecoration(DividerItemDecoration(recyclerHome.context,(layoutManager as LinearLayoutManager).orientation))




               }
           }
            } catch (e: JSONException) {
            e.printStackTrace()
        }
        },
        Response.ErrorListener { error: VolleyError? ->
            Toast.makeText(activity as Context, error?.message, Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers=HashMap<String,String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "28c1eaad0887fc"
               return headers

            }
        }
        queue.add(jsonObjectRequest)
        }
        else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            builder.create()
            builder.show()
        }

        return view

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}