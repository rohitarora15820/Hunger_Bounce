package com.rohit.hungerbounce.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.database.RestaurantDatabase
import com.rohit.hungerbounce.database.RestaurantEntity
import com.rohit.hungerbounce.fragment.RestaurantFragment
import com.rohit.hungerbounce.model.Restaurants
import com.squareup.picasso.Picasso

class HomeRecyclerAdapter(
    var itemList:ArrayList<Restaurants>,
    var context: Context
):RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {
    class HomeViewHolder(view: View):RecyclerView.ViewHolder(view){
       // val textView:TextView=view.findViewById(R.id.txtRecyclerRowItem)
        val txtRestaurantName:TextView=view.findViewById(R.id.txtRestaurantName)
        val txtRestaurantRating:TextView=view.findViewById(R.id.txtRating)
        val txtRestaurantCost:TextView=view.findViewById(R.id.txtCost)

        val ImageRestaurant:ImageView=view.findViewById(R.id.imgRestaurant)
        val llContent:LinearLayout=view.findViewById(R.id.llContent)
        val favImage = view.findViewById(R.id.imgFav) as ImageView


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row,parent,false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val text=itemList[position]
       // holder.textView.text=text
        holder.txtRestaurantName.text=text.name
        holder.txtRestaurantRating.text=text.rating
        holder.txtRestaurantCost.text=text.costForOne.toString()
        Picasso.get().load(text.imageUrl).into(holder.ImageRestaurant)
        holder.llContent.setOnClickListener{
            val fragment = RestaurantFragment()
            val args = Bundle()
            args.putInt("id", text.id)
            args.putString("name", text.name)
            fragment.arguments = args
            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, fragment)
            transaction.commit()
            (context as AppCompatActivity).supportActionBar?.title = holder.txtRestaurantName.text.toString()
            val listOfFavourites = GetAllFavAsyncTask(context).execute().get()

            if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(text.id.toString())) {
                holder.favImage.setImageResource(R.drawable.ic_action_fav_checked)
            } else {
                holder.favImage.setImageResource(R.drawable.ic_action_fav)
            }

            holder.favImage.setOnClickListener {
                val restaurantEntity = RestaurantEntity(
                    text.id,
                    text.name,
                    text.rating,
                    text.costForOne.toString(),
                    text.imageUrl
                )

                if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                    val async =
                        DBAsyncTask(context, restaurantEntity, 2).execute()
                    val result = async.get()
                    if (result) {
                        holder.favImage.setImageResource(R.drawable.ic_action_fav_checked)
                    }
                } else {
                    val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                    val result = async.get()

                    if (result) {
                        holder.favImage.setImageResource(R.drawable.ic_action_fav)
                    }
                }
            }
        }


    }
    class DBAsyncTask(context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            /*
            Mode 1 -> Check DB if the book is favourite or not
            Mode 2 -> Save the book into DB as favourite
            Mode 3 -> Remove the favourite book
            */

            when (mode) {

                1 -> {
                    val res: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false

        }

    }


    /*Since the outcome of the above background method is always a boolean, we cannot use the above here.
    * We require the list of favourite restaurants here and hence the outcome would be list.
    * For simplicity we obtain the list of restaurants and then extract their ids which is then compared to the ids
    * inside the list sent to the adapter */

    class GetAllFavAsyncTask(
        context: Context
    ) :
        AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<String> {

            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.id.toString())
            }
            return listOfIds
        }
    }
}