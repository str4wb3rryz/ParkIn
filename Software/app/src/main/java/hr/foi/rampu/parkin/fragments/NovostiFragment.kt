package hr.foi.rampu.parkin.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.rampu.parkin.R
import hr.foi.rampu.parkin.adapters.NovostiAdapter
import hr.foi.rampu.parkin.helpers.MockDataLoaderForNovosti


class NovostiFragment : Fragment() {
    private val mockTasksNovosti = MockDataLoaderForNovosti.getDemoData()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mockTasksNovosti.forEach { Log.i("MOCK_PENDING_TASKS", it.name) }
        return inflater.inflate(R.layout.fragment_novosti, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rv_novosti)
        recyclerView.adapter = NovostiAdapter(MockDataLoaderForNovosti.getDemoData())
        recyclerView.layoutManager = LinearLayoutManager(view.context)
    }


}