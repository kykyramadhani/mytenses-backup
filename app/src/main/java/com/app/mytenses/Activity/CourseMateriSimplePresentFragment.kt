import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.mytenses.Activity.Chapter1LessonFragment
import com.app.mytenses.Activity.Chapter2FormulaFragment
import com.app.mytenses.Activity.Chapter3ExampleFragment
import com.app.mytenses.R

class CourseMateriSimplePresent : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_materi_simple_present, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol kembali: skip CourseRingkasan jika ada di backstack
        view.findViewById<ImageButton>(R.id.backButtonMateriSimplePresent)?.setOnClickListener {
            val fm = requireActivity().supportFragmentManager
            val count = fm.backStackEntryCount

            for (i in count - 1 downTo 0) {
                val entry = fm.getBackStackEntryAt(i)
                if (entry.name == "CourseRingkasan") {
                    fm.popBackStack(entry.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    break
                }
            }

            // Pop ke fragment sebelumnya
            fm.popBackStack()
        }

        // Tab Ringkasan -> Ganti fragment ke CourseRingkasanSimplePresent
        val tabRingkasan = view.findViewById<TextView>(R.id.tabRingkasan)
        tabRingkasan.setOnClickListener {
            val fragment = CourseRingkasanSimplePresent()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
//                .addToBackStack("CourseRingkasan") // <--- penting!
                .commit()
        }

        // Tombol Chapter 1
        val btnChapter1 = view.findViewById<Button>(R.id.btnChapter1)
        btnChapter1.setOnClickListener {
            val fragment = Chapter1LessonFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Tombol Chapter 2
        val btnChapter2 = view.findViewById<Button>(R.id.btnChapter2)
        btnChapter2.setOnClickListener {
            val fragment = Chapter2FormulaFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Tombol Chapter 3
        val btnChapter3 = view.findViewById<Button>(R.id.btnChapter3)
        btnChapter3.setOnClickListener {
            val fragment = Chapter3ExampleFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        val btnBelajar1 = view.findViewById<Button>(R.id.btnBelajar1)
        btnBelajar1.setOnClickListener {
            val fragment = Chapter1LessonFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            }
    }
}