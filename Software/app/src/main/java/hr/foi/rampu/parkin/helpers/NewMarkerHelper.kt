package hr.foi.rampu.parkin.helpers

import android.app.TimePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import hr.foi.rampu.parkin.R

class NewMarkerHelper (private val view: View) {
    private val forStartHours: EditText =  view.findViewById(R.id.fragment_add_location_et_working_hours_start)
    private val forEndHours: EditText = view.findViewById(R.id.fragment_add_location_et_working_hours_stop)
    private val spZone: Spinner = view.findViewById(R.id.fragment_add_location_sp_zone)
    private val spType: Spinner = view.findViewById(R.id.fragment_add_location_sp_type)



    fun populateZoneSpinner() {
        val zoneOptions = listOf("Zone 0","Zone 1", "Zone 2", "Zone 3")
        val adapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_item,
            zoneOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spZone.adapter = adapter
    }
    fun populateTypeSpinner() {
        val typeOptions = listOf(
            "On-street",
            "Parking lot",
            "Parking garage",
            "Fringe parking",
            "Carport"
        )
        val adapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_item,
            typeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = adapter
    }
    fun activateTimeListeners(){
        forStartHours.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                TimePickerDialog(
                    view.context,
                    { view, hour, minute ->
                        val formattedHour = String.format("%02d", hour)
                        val formattedMinute = String.format("%02d", minute)

                        forStartHours.setText(
                            "$formattedHour:$formattedMinute"
                        )
                        /*forStartHours.setText(
                            "$hour:$minute"
                        )*/
                    },
                    16,
                    15,
                    true
                ).show()
                v.clearFocus()
            }
        }

        forEndHours.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                TimePickerDialog(
                    view.context,
                    { view, hour, minute ->
                        val formattedHour = String.format("%02d", hour)
                        val formattedMinute = String.format("%02d", minute)

                        forEndHours.setText(
                            "$formattedHour:$formattedMinute"
                        )
                        /*forEndHours.setText(
                            "$hour:$minute"
                        )*/
                    },
                    16,
                    15,
                    true
                ).show()
                v.clearFocus()
            }
        }
    }
}