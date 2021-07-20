package com.rohit.hungerbounce.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.activity.CartActivity
import com.rohit.hungerbounce.adapter.MenuAdapter
import com.rohit.hungerbounce.database.OrderEntity
import com.rohit.hungerbounce.database.RestaurantDatabase
import com.rohit.hungerbounce.fragment.RestaurantFragment.Companion.resId
import com.rohit.hungerbounce.model.FoodItem
import com.rohit.hungerbounce.utility.ConnectionManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RestaurantFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RestaurantFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantMenuAdapter: MenuAdapter
    private var menuList = arrayListOf<FoodItem>()
    private lateinit var rlLoading: RelativeLayout
    private var orderList = arrayListOf<FoodItem>()
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: Int? = 0
        var resName: String? = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_restaurant, container, false)
        sharedPreferences =
            activity?.getSharedPreferences("FoodApp", Context.MODE_PRIVATE) as SharedPreferences
        rlLoading = view?.findViewById(R.id.progressLayout) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        resId = arguments?.getInt("id", 0)
        resName = arguments?.getString("name", "")
      //  (activity as DrawerLocker).setDrawerEnabled(false)
        setHasOptionsMenu(true)
        goToCart = view.findViewById(R.id.buttonRestaurant) as Button
        goToCart.visibility = View.GONE
        goToCart.setOnClickListener {
            proceedToCart()
          //  Toast.makeText(activity as Context,"Item added",Toast.LENGTH_SHORT).show()
        }

    setUpRestaurantMenu(view)

        return view
    }




private fun setUpRestaurantMenu(view: View) {

    recyclerMenu = view.findViewById(R.id.recyclerViewRestaurant)
    if (ConnectionManager().checkConnectivity(activity as Context)) {

        val queue = Volley.newRequestQueue(activity as Context)
        val url="http://13.235.250.119/v2/restaurants/fetch_result/"

        val jsonObjectRequest = object :
            JsonObjectRequest(Method.GET, url + resId , null, Response.Listener {
                rlLoading.visibility = View.GONE

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val resArray = data.getJSONArray("data")
                        for (i in 0 until resArray.length()) {
                            val menuObject = resArray.getJSONObject(i)
                            val foodItem = FoodItem(
                                menuObject.getString("id"),
                                menuObject.getString("name"),
                                menuObject.getString("cost_for_one").toInt()
                            )
                            menuList.add(foodItem)
                            restaurantMenuAdapter = MenuAdapter(
                                activity as Context,
                                menuList,
                                object : MenuAdapter.OnItemClickListener {
                                    override fun onAddItemClick(foodItem: FoodItem) {
                                        orderList.add(foodItem)
                                        if (orderList.size > 0) {
                                            goToCart.visibility = View.VISIBLE
                                            MenuAdapter.isCartEmpty = false
                                        }
                                    }

                                    override fun onRemoveItemClick(foodItem: FoodItem) {
                                        orderList.remove(foodItem)
                                        if (orderList.isEmpty()) {
                                            goToCart.visibility = View.GONE
                                            MenuAdapter.isCartEmpty = true
                                        }
                                    }
                                })
                            val mLayoutManager = LinearLayoutManager(activity)
                            recyclerMenu.layoutManager = mLayoutManager
                            recyclerMenu.itemAnimator = DefaultItemAnimator()
                            recyclerMenu.adapter = restaurantMenuAdapter
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"


                headers["token"] = "28c1eaad0887fc"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    } else {
        Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
    }
}


private fun proceedToCart() {

    /*Here we see the implementation of Gson.
    * Whenever we want to convert the custom data types into simple data types
    * which can be transferred across for utility purposes, we will use Gson*/
    val gson = Gson()

    /*With the below code, we convert the list of order items into simple string which can be easily stored in DB*/
    val foodItems = gson.toJson(orderList)

    val async = ItemsOfCart(activity as Context, resId.toString(), foodItems, 1).execute()
    val result = async.get()
    if (result) {
        val data = Bundle()
        data.putInt("resId", resId as Int)
        data.putString("resName", resName)
        val intent = Intent(activity, CartActivity::class.java)
        intent.putExtra("data", data)
        startActivity(intent)
    } else {
        Toast.makeText((activity as Context), "Some unexpected error", Toast.LENGTH_SHORT)
            .show()
    }

}
    class ItemsOfCart(
        context: Context,
        val restaurantId: String,
        val foodItems: String,
        val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()


        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
            }

            return false
        }

    }

}
