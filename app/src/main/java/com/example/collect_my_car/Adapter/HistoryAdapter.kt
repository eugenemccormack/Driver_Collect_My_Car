package com.example.collect_my_car.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collect_my_car.Model.TripPlanModel
import com.example.collect_my_car.R
import kotlinx.android.synthetic.main.activity_recyclerview_layout.view.*

class HistoryAdapter (val context: Context, val posts: List<TripPlanModel>, private val listener: OnItemClickListener): RecyclerView.Adapter<HistoryAdapter.MyViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.MyViewHolder {

        val itemView = LayoutInflater.from(context).inflate(R.layout.activity_recyclerview_layout, parent, false)

        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: HistoryAdapter.MyViewHolder, position: Int) {

        holder.bind(posts[position])

    }

    override fun getItemCount() = posts.size
    //) /*(internal var context: Context): RecyclerView.Adapter<DriverAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        fun bind(tripPlanModel: TripPlanModel) {

            itemView.id_history.text = tripPlanModel.collectionNumber//collectionsPhotos!!.photos
            itemView.date_history.text = tripPlanModel.time//userModel.toString() //tripPlanModel.userModel!!.name //Users Name
            itemView.from_history.text = tripPlanModel.originString


            Log.d("Adapter", "TIME" + tripPlanModel.time)

            Log.d("Adapter", "USER" + tripPlanModel.collectionNumber)
            //Glide.with(context).load(tripPlanModel.userModel!!.image).into(itemView.image)


        }
        init{

            itemView.setOnClickListener(this)

        }


        override fun onClick(v: View?) {

            val position = adapterPosition

            if(position != RecyclerView.NO_POSITION) {

                listener.onItemClick(position)

            }

        }


    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)


    }


}