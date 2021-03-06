package com.example.lababsencesystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StudentMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView studentWelcome;
    public static Student student;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    Preference preference=new Preference();
    public static ArrayList<Course> courses = new ArrayList<>();
    static ArrayList<String> coursesCode = new ArrayList<>();

    TextView headerName,headerEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//       studentWelcome = findViewById(R.id.studetWelcome);
//       toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.studentDrawer);
        navigationView = findViewById(R.id.navigationMenuStudent);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//      navigationView.setOnNavigationItemSelectedListener(navListener);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        headerName = headView.findViewById(R.id.headerName);
        headerEmail = headView.findViewById(R.id.headerEmail);

        if(student==null) {
            db.collection("users").document("students")
                    .collection("data")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("email").equals(firebaseUser.getEmail())) {
                                student = document.toObject(Student.class);
                                loadCourses();
                                headerEmail.setText(student.getEmail());
                                headerName.setText(student.getName());
                                break;
                            }
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                }
            });
        }else{
            loadCourses();
            headerEmail.setText(student.getEmail());
            headerName.setText(student.getName());
        }


    }

    private void loadCourses() {
        db.collection("courses").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    courses.clear();
                    coursesCode.clear();
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        checkCourse(document.getId(), document.toObject(Course.class));
//                        Course course = document.toObject(Course.class);
//                        courses.add(course);
//                        coursesCode.add(course.getCode());
//                        StudentHomeFragment.a.notifyDataSetChanged();

                    }
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new StudentHomeFragment()).commit();
                }
            }
        });
    }

    private void checkCourse(String id, final Course course) {
        db.collection("courses").document(id).collection("students").whereEqualTo("fileNumber", student.getFileNumber()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        if (doc.get("fileNumber").toString().equals(student.getFileNumber())) {
                        courses.add(course);
                        coursesCode.add(course.getCode());
//                        StudentHomeFragment.a.notifyDataSetChanged();
//                        }
//                    }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int logout =0;
        Fragment selectedFragment = null;
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.menuStudentHome:
                selectedFragment = new StudentHomeFragment();
                break;
            case R.id.menuStudentCourses:
                selectedFragment = new StudentCoursesFragment();
                break;
            case R.id.menuStudentLabs:
                selectedFragment = new StudentLabsFragment();
                break;
            case R.id.menuStudentTakeAttendance:
                selectedFragment = new StudentHomeFragment();
                Intent intent = new Intent(this, QRCodeScanner.class);
                startActivity(intent);
                break;
            case R.id.menuStudentProfile:
                selectedFragment = new StudentProfileFragment();
                break;
            case R.id.menuStudentLogout:
                logout =1;
                FirebaseAuth.getInstance().signOut();
                student=null;
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
        }
        if (logout==0)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        return true;

    }
}