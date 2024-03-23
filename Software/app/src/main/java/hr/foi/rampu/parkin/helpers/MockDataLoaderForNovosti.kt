package hr.foi.rampu.parkin.helpers

import hr.foi.rampu.parkin.entities.TaskCategoryForNovosti
import hr.foi.rampu.parkin.entities.TaskForNovosti


object MockDataLoaderForNovosti {
    fun getDemoData(): MutableList<TaskForNovosti> {
        val categoriesNovosti= getDemoCategoriesNovosti()
        return mutableListOf(
            TaskForNovosti("Prometna nesreća u blizini zone 1", categoriesNovosti[0]),
            TaskForNovosti("Besplatan parking na Banus placu", categoriesNovosti[1]),
            TaskForNovosti("U Varaždinu novi sustav naplate parkinga", categoriesNovosti[2])
        )
    }
    fun getDemoCategoriesNovosti(): List<TaskCategoryForNovosti> = listOf(
        TaskCategoryForNovosti("NovostiOprez","#D2042D" ),
        TaskCategoryForNovosti("NovostiLife","#BF40BF" ),
        TaskCategoryForNovosti("NovostiCijene","#5D3FD3" )
    )

}