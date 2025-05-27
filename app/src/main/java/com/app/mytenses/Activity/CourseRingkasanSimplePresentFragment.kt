import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.mytenses.R
import com.app.mytenses.Activity.Chapter1LessonActivity
import com.app.mytenses.Activity.Chapter1LessonFragment

class CourseRingkasanSimplePresent : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_ringkasan_simple_present, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol kembali
        view.findViewById<ImageButton>(R.id.backButton)?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Tab Materi -> Ganti fragment
        val tabMateri = view.findViewById<TextView>(R.id.tabMateri)
        tabMateri.setOnClickListener {
            // Ini setara dengan startActivity dalam Fragment
            val fragment = CourseMateriSimplePresent()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Agar bisa kembali dengan tombol back
                .commit()
        }

        val btnMulaiBelajar = view.findViewById<Button>(R.id.btnBelajar)
        btnMulaiBelajar.setOnClickListener {
            // Ini setara dengan startActivity dalam Fragment
            val fragment = Chapter1LessonFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Agar bisa kembali dengan tombol back
                .commit()
        }


    }
}