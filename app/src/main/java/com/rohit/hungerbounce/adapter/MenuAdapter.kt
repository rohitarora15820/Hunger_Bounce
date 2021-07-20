package com.rohit.hungerbounce.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.model.FoodItem

class MenuAdapter(val context: Context,
                  private val menuList: ArrayList<FoodItem>,
                  private val listener: OnItemClickListener
): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodItemName: TextView = view.findViewById(R.id.txtItemName)
        val foodItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val sno: TextView = view.findViewById(R.id.txtSNo)
        val addToCart: Button = view.findViewById(R.id.btnAddToCart)
        val removeFromCart: Button = view.findViewById(R.id.btnRemoveFromCart)

    }
    companion object {
        var isCartEmpty = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_menu_single_row, parent, false)

        return MenuViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }
    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItem)
        fun onRemoveItemClick(foodItem: FoodItem)
    }

    override fun onBindViewHolder(p0: MenuViewHolder, p1: Int) {
        val menuObject = menuList[p1]
        p0.foodItemName.text = menuObject.name
        val cost = "Rs. ${menuObject.cost?.toString()}"
        p0.foodItemCost.text = cost
        p0.sno.text = (p1 + 1).toString()
        p0.addToCart.setOnClickListener {
            p0.addToCart.visibility = View.GONE
            p0.removeFromCart.visibility = View.VISIBLE
            listener.onAddItemClick(menuObject)
        }

        p0.removeFromCart.setOnClickListener {
            p0.removeFromCart.visibility = View.GONE
            p0.addToCart.visibility = View.VISIBLE
            listener.onRemoveItemClick(menuObject)


    }
}
    override fun getItemViewType(position: Int): Int {
        return position
    }}