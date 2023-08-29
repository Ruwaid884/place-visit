package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import org.w3c.dom.Text

open class HappyPlacesAdapter(private val context: Context,
     private var list: ArrayList<HappyPlaceModel>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var onClickListner:OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {



        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_happy_place,
        parent,
        false)
        )

    }

    fun setOnClickListner(onClickListner: OnClickListener){
        this.onClickListner=onClickListner
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if(holder is MyViewHolder){
            holder.image.setImageURI(Uri.parse(model.image))
            holder.title.text = model.title
            holder.description.text = model.description
            holder.itemView.setOnClickListener{
               onClickListner?.onClick(position,model)
            }
        }
    }

    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDelete = dbHandler.deleteHappyPlace(list[position])

        if(isDelete >0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(activity: Activity,position: Int,requestCode: Int){
        val intent = Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
       return list.size
    }


    interface OnClickListener{
        fun onClick(position: Int,model: HappyPlaceModel)
    }



    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val image :ImageView = view.findViewById(R.id.iv_place_image)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description:TextView = view.findViewById(R.id.tvDescription)
    }

}