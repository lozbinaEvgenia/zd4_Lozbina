import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings.System.DATE_FORMAT
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.fragment.app.Fragment
import com.example.crimeintent.R
import com.example.crimeintent.Crime


class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var suspectButton: Button
    private lateinit var reportButton: Button
    private lateinit var callButton: Button

    private val REQUEST_CALL_PERMISSION = 1

    var phone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        callButton = view.findViewById(R.id.call_suspect) as Button

        dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked


            val message = if (isChecked) {
                "Раскрыто"
            } else {
                "Не раскрыто"
            }


            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        reportButton.setOnClickListener {
            if (!titleField.getText().toString().isEmpty()){
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
            }
            else Toast.makeText(context, "Заполните преступление", Toast.LENGTH_SHORT).show()
        }

        callButton.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${phone}")
            }

            if (requireContext().checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
            }
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields =
                    arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
                if (contactUri != null) {
                    val cursor = requireActivity().contentResolver.query(
                        contactUri,
                        queryFields,
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.count > 0) {
                            it.moveToFirst()
                            val suspect =
                                it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            crime.suspect = suspect
                            suspectButton.text = suspect


                            val contactId =
                                it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                            val phoneCursor = requireActivity().contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )

                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    phone =
                                        pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                }
                            }
                        }
                    }
                } else {
                }
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
        private const val REQUEST_CONTACT = 1
        private const val DATE_FORMAT = "EEE, MMM, dd"
    }
}