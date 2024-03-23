package hr.foi.rampu.parkin.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.entities.TaskCategoryForNovosti
import hr.foi.rampu.parkin.entities.TaskForNovosti
import hr.foi.rampu.parkin.helpers.MockDataLoaderForNovosti


class NovostiAdapter(private val novostiList : List<TaskForNovosti>) : RecyclerView.Adapter<NovostiAdapter.NovostiViewHolder>() {

    inner class NovostiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val novostiName: TextView
        private val novostiCategoryColor:View
        init {
            novostiName = view.findViewById(R.id.task_naziv_novosti)
            novostiCategoryColor=view.findViewById(R.id.task_category_novosti_boja)
        }
        fun bind(novosti: TaskForNovosti) {
            novostiName.text = novosti.name
            novostiCategoryColor.setBackgroundColor(novosti.category.color.toColorInt())
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovostiViewHolder {
        val taskView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.novosti_lista, parent, false)
        return NovostiViewHolder(taskView)
    }

    override fun onBindViewHolder(holder: NovostiViewHolder, position: Int) {
        holder.bind(novostiList[position])
    }
    override fun getItemCount() = novostiList.size
}