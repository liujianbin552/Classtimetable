package com.example.classtimetable;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout day;

    private DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);
    int currentCoursesNumber = 0;
    int maxCoursesNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadData();
    }

    private void loadData() {
        ArrayList<Course> coursesList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from courses", null);
        if (cursor.moveToFirst()) {
            do {
                coursesList.add(new Course(
                        cursor.getString(cursor.getColumnIndex("course_name")),
                        cursor.getString(cursor.getColumnIndex("teacher")),
                        cursor.getString(cursor.getColumnIndex("class_room")),
                        cursor.getInt(cursor.getColumnIndex("day")),
                        cursor.getInt(cursor.getColumnIndex("class_start")),
                        cursor.getInt(cursor.getColumnIndex("class_end"))));
            } while(cursor.moveToNext());
        }
        cursor.close();

        for (Course course : coursesList) {
            createLeftView(course);
            createCourseView(course);
        }
    }

    private void saveData(Course course) {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL
                ("insert into courses(course_name, teacher, class_room, day, class_start, class_end) " + "values(?, ?, ?, ?, ?, ?)",
                new String[] {course.getCourseName(),
                        course.getTeacher(),
                        course.getClassRoom(),
                        course.getDay()+"",
                        course.getStart()+"",
                        course.getEnd()+""}
                );
    }

    private void createLeftView(Course course) {
        int len = course.getEnd();
        if (len > maxCoursesNumber) {
            for (int i = 0; i < len-maxCoursesNumber; i++) {
                View view = LayoutInflater.from(this).inflate(R.layout.left_view, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110,180);
                view.setLayoutParams(params);

                TextView text = view.findViewById(R.id.class_number_text);
                text.setText(String.valueOf(++currentCoursesNumber));

                LinearLayout leftViewLayout = findViewById(R.id.left_view_layout);
                leftViewLayout.addView(view);
            }
        }
        maxCoursesNumber = len;
    }

    private void createCourseView(final Course course) {
        int height = 180;
        int getDay = course.getDay();
        if ((getDay < 1 || getDay > 7) || course.getStart() > course.getEnd())
            Toast.makeText(this, "填写错误", Toast.LENGTH_LONG).show();
        else {
            switch (getDay) {
                case 1: day = findViewById(R.id.monday); break;
                case 2: day = findViewById(R.id.tuesday); break;
                case 3: day = findViewById(R.id.wednesday); break;
                case 4: day = findViewById(R.id.thursday); break;
                case 5: day = findViewById(R.id.friday); break;
                case 6: day = findViewById(R.id.saturday); break;
                case 7: day = findViewById(R.id.sunday); break;
            }
            final View v = LayoutInflater.from(this).inflate(R.layout.course_card, null);
            v.setY(height * (course.getStart()-1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT,(course.getEnd()-course.getStart()+1)*height - 8);
            v.setLayoutParams(params);
            TextView text = v.findViewById(R.id.text_view);
            text.setText(course.getCourseName() + "\n" + course.getTeacher() + "\n" + course.getClassRoom());
            day.addView(v);
            //长按删除课程
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setVisibility(View.GONE);
                    day.removeView(v);
                    SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
                    sqLiteDatabase.execSQL("delete from courses where course_name = ?", new String[] {course.getCourseName()});
                    return true;
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 0 && data != null) {
            Course course = (Course) data.getSerializableExtra("course");
            createLeftView(course);
            createCourseView(course);
            saveData(course);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_courses:
                Intent intent = new Intent(MainActivity.this, AddCourseActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
        return true;
    }
}