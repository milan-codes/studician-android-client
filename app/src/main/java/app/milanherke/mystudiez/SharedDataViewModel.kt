package app.milanherke.mystudiez

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {

    val subjectFilter = MutableLiveData<String>()
}