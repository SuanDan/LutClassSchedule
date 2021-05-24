package com.example.hhuclassschedule;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.timetable.model.Schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

import static com.example.hhuclassschedule.MainActivity.toGetSubjects;
import static com.example.hhuclassschedule.MainActivity.toSaveSubjects;

public class AddCourseActivity extends AppCompatActivity {

    private static final String TAG = "AddCourseActivity";

    String title;
    List<Schedule> scheduletList;

    LinearLayout ll_addCourse;
    EditText et_courseName;
    RelativeLayout rl_indlude_detail;
    TextView et_weeks;
    TextView et_time;
    EditText et_teacher;
    EditText et_room;
    TextView tv_ib_delete;

    int day, start, step;
    String name, position, teacher;
    List<Integer> weeks;

    NumberPickerView dayPicker;
    NumberPickerView sectionStartPicker;
    NumberPickerView sectionEndPicker;
    NumberPickerView weekStartPicker;
    NumberPickerView weekEndPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        title = (String) getIntent().getExtras().get("title");
        initToolbar(title);
        if (title.equals("编辑课程")) {
            String scheduleJson = getIntent().getStringExtra("scheduleList");
            scheduletList = new Gson().fromJson(scheduleJson,new TypeToken<List<Schedule>>(){}.getType());
            editSubject(scheduletList);
        } else {
            int i_day = (int) getIntent().getExtras().get("day");
            int i_start = (int) getIntent().getExtras().get("start");
            addSubject(i_day, i_start);
        }
    }

    protected void initToolbar(String title) {

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 设置title
        TextView textView = findViewById(R.id.toolbar_title);
        textView.setText(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.savemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.btn_save_course) {

            name = et_courseName.getText().toString();
            position = et_room.getText().toString();
            teacher = et_teacher.getText().toString();

            if (name == null || name.length() == 0) {
                Toast.makeText(AddCourseActivity.this, "请输入课程名！"+name, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (weeks == null || weeks.isEmpty()) {
                Toast.makeText(AddCourseActivity.this, "请选择周数！", Toast.LENGTH_SHORT).show();
                return false;
            }

            List<MySubject> mySubjects = toGetSubjects();
            if (title.equals("编辑课程")) {
                int delete_id = Double.valueOf(String.valueOf(scheduletList.get(0).getExtras().get("extras_id"))).intValue();
                Iterator<MySubject> iterator = mySubjects.iterator();
                while (iterator.hasNext()) {
                    MySubject next = iterator.next();
                    int id = next.getId();
                    if (id == delete_id) {
                        iterator.remove();
                        break;
                    }
                }
            }
            if(null == mySubjects){
                mySubjects = new ArrayList<>();
            }
            mySubjects.add(new MySubject(null, name, position, teacher, weeks, start, step, day, -1, null));
            toSaveSubjects(mySubjects);
            Intent intent = new Intent(AddCourseActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 销毁当前activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 编辑课程
    protected void editSubject(List<Schedule> beans) {
        ll_addCourse = findViewById(R.id.ll_add_course_detail);
        // 课程名
        et_courseName = findViewById(R.id.et_name);
        et_courseName.setText(beans.get(0).getName());

        rl_indlude_detail = ll_addCourse.findViewById(R.id.include_add_course_detail);
        // 周数
        et_weeks = rl_indlude_detail.findViewById(R.id.et_weeks);
        weeks = beans.get(0).getWeekList();
        String str_weeks = "第" + beans.get(0).getWeekList().get(0) + "-" + beans.get(0).getWeekList().get(beans.get(0).getWeekList().size() - 1) + "周";
        et_weeks.setText(str_weeks);
        et_weeks.setClickable(true);
        et_weeks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWeek();
            }
        });
        // 节数
        String[] arrayday = {"一", "二", "三", "四", "五", "六", "日"};
        et_time = rl_indlude_detail.findViewById(R.id.et_time);
        day = beans.get(0).getDay();
        start = beans.get(0).getStart();
        step = beans.get(0).getStep();
        String str_time = "周" + arrayday[beans.get(0).getDay() - 1] + "   第" + beans.get(0).getStart() + "-" + (beans.get(0).getStart() + beans.get(0).getStep() - 1) + "节";
        et_time.setText(str_time);
        et_time.setClickable(true);
        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime();
            }
        });
        // 老师
        et_teacher = rl_indlude_detail.findViewById(R.id.et_teacher);
        et_teacher.setText(beans.get(0).getTeacher());
        // 教室
        et_room = rl_indlude_detail.findViewById(R.id.et_room);
        et_room.setText(beans.get(0).getRoom());

        // 删除时间段
        tv_ib_delete = rl_indlude_detail.findViewById(R.id.ib_delete);
        tv_ib_delete.setClickable(true);
        tv_ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "至少要保留一个时间段";
                Toast.makeText(AddCourseActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 添加课程
    protected void addSubject(int i_day, int i_start) {

        day = i_day+1;
        start = i_start;
        step = 2;
        weeks = new ArrayList<>();

        ll_addCourse = findViewById(R.id.ll_add_course_detail);
        // 课程名
        et_courseName = findViewById(R.id.et_name);
        rl_indlude_detail = ll_addCourse.findViewById(R.id.include_add_course_detail);
        // 周数
        et_weeks = rl_indlude_detail.findViewById(R.id.et_weeks);
        et_weeks.setClickable(true);
        et_weeks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWeek();
            }
        });
        // 节数
        et_time = rl_indlude_detail.findViewById(R.id.et_time);
        String[] arrayday = {"一", "二", "三", "四", "五", "六", "日"};
        i_start = i_start % 2 == 0 ? i_start - 1 : i_start;
        String str_time = "周" + arrayday[i_day] + "   第" + i_start + "-" + (i_start + 1) + "节";
        et_time.setText(str_time);
        et_time.setClickable(true);
        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime();
            }
        });
        // 老师
        et_teacher = rl_indlude_detail.findViewById(R.id.et_teacher);
        // 教室
        et_room = rl_indlude_detail.findViewById(R.id.et_room);
        // 删除时间段
        tv_ib_delete = rl_indlude_detail.findViewById(R.id.ib_delete);
        tv_ib_delete.setClickable(true);
        tv_ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "至少要保留一个时间段";
                Toast.makeText(AddCourseActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 选择时间
    protected void selectTime() {
        View selectTimeDetail = getLayoutInflater().inflate(R.layout.fragment_select_time, null);
        initTimePicker(selectTimeDetail);
        // 设置自定义布局
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(selectTimeDetail);
        final AlertDialog dialog = builder.show();

        Button btn_cancel = selectTimeDetail.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_savetime = selectTimeDetail.findViewById(R.id.btn_save_time);
        btn_savetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = dayPicker.getValue() + 1;
                start = sectionStartPicker.getValue() + 1;
                step = sectionEndPicker.getValue() - sectionStartPicker.getValue() + 1;
                String[] arrayday = {"一", "二", "三", "四", "五", "六", "日"};
                String str_time = "周" + arrayday[day - 1] + "   第" + start + "-" + (sectionEndPicker.getValue() + 1) + "节";
                et_time.setText(str_time);
                dialog.dismiss();
            }
        });
    }

    protected void initTimePicker(View selectTimeDetail) {
        dayPicker = selectTimeDetail.findViewById(R.id.time_day);
        String[] week = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        dayPicker.setDisplayedValues(week);
        //设置最大值
        dayPicker.setMaxValue(week.length - 1);
        //设置最小值
        dayPicker.setMinValue(0);
        //设置当前值
        dayPicker.setValue(day-1);
        //设置滑动监听


        sectionStartPicker = selectTimeDetail.findViewById(R.id.time_start);
        String[] sectionStart = {"第1节", "第2节", "第3节", "第4节", "第5节", "第6节", "第7节", "第8节", "第9节", "第10节", "第11节", "第12节", "第13节", "第14节", "第15节"};
        sectionStartPicker.setDisplayedValues(sectionStart);
        //设置最大值
        sectionStartPicker.setMaxValue(sectionStart.length - 1);
        //设置最小值
        sectionStartPicker.setMinValue(0);
        //设置当前值
        sectionStartPicker.setValue(start-1);


        sectionEndPicker = selectTimeDetail.findViewById(R.id.time_end);
        String[] sectionEnd = {"第1节", "第2节", "第3节", "第4节", "第5节", "第6节", "第7节", "第8节", "第9节", "第10节", "第11节", "第12节", "第13节", "第14节", "第15节"};
        sectionEndPicker.setDisplayedValues(sectionEnd);
        //设置最大值
        sectionEndPicker.setMaxValue(sectionEnd.length - 1);
        //设置最小值
        sectionEndPicker.setMinValue(0);
        //设置当前值
        sectionEndPicker.setValue(start+step-2);

        sectionStartPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                if(newVal>sectionEndPicker.getValue()){
                    sectionEndPicker.setValue(sectionStartPicker.getValue());
                    sectionEndPicker.smoothScrollToValue(sectionStartPicker.getValue(),false);
                }
                String toast = oldVal + " " + newVal;
                Toast.makeText(AddCourseActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
        sectionEndPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                if(newVal<sectionStartPicker.getValue()){
                    sectionStartPicker.setValue(sectionEndPicker.getValue());
                    sectionStartPicker.smoothScrollToValue(sectionEndPicker.getValue(),false);
                }
                String toast = oldVal + " " + newVal;
                  Toast.makeText(AddCourseActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 选择周数
    protected void selectWeek() {
        View selectWeekDetail = getLayoutInflater().inflate(R.layout.fragment_select_week, null);
        initWeekPicker(selectWeekDetail);
        // 设置自定义布局
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(selectWeekDetail);
        final AlertDialog dialog = builder.show();

        Button btn_cancel = selectWeekDetail.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_saveWeek = selectWeekDetail.findViewById(R.id.btn_save_week);
        btn_saveWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = weekStartPicker.getValue() + 1;
                int end = weekEndPicker.getValue() + 1;
                weeks = new ArrayList<Integer>();
                for (int i = start; i <= end; i++) {
                    weeks.add(i);
                }
                String str_weeks = "第" + start + "-" + end + "周";
                et_weeks.setText(str_weeks);
                dialog.dismiss();
            }
        });

    }

    protected void initWeekPicker(View selectWeekDetail) {

        weekStartPicker = selectWeekDetail.findViewById(R.id.week_start);
        String[] weekStart = {"第1周", "第2周", "第3周", "第4周", "第5周", "第6周", "第7周", "第8周", "第9周", "第10周", "第11周", "第12周", "第13周", "第14周", "第15周",
                "第16周", "第17周", "第18周", "第19周", "第20周", "第21周", "第22周", "第23周", "第24周", "第25周"};
        weekStartPicker.setDisplayedValues(weekStart);
        //设置最大值
        weekStartPicker.setMaxValue(weekStart.length - 1);
        //设置最小值
        weekStartPicker.setMinValue(0);
        //设置当前值
        if(weeks.isEmpty()){
            weekStartPicker.setValue(0);
        }else {
            weekStartPicker.setValue(weeks.get(0)-1);
        }


        weekEndPicker = selectWeekDetail.findViewById(R.id.week_end);
        String[] weekEnd = {"第1周", "第2周", "第3周", "第4周", "第5周", "第6周", "第7周", "第8周", "第9周", "第10周", "第11周", "第12周", "第13周", "第14周", "第15周",
                "第16周", "第17周", "第18周", "第19周", "第20周", "第21周", "第22周", "第23周", "第24周", "第25周"};
        weekEndPicker.setDisplayedValues(weekEnd);
        //设置最大值
        weekEndPicker.setMaxValue(weekEnd.length - 1);
        //设置最小值
        weekEndPicker.setMinValue(0);
        //设置当前值
        if(weeks.isEmpty()){
            weekEndPicker.setValue(0);
        }else {
            weekEndPicker.setValue(weeks.get(weeks.size()-1)-1);
        }

        weekStartPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                if(newVal>weekEndPicker.getValue()){
                    weekEndPicker.setValue(weekStartPicker.getValue());
                    weekEndPicker.smoothScrollToValue(weekStartPicker.getValue(),false);
                }
            }
        });

        weekEndPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                if(newVal<weekStartPicker.getValue()){
                    weekStartPicker.setValue(weekEndPicker.getValue());
                    weekStartPicker.smoothScrollToValue(weekEndPicker.getValue(),false);
                }
            }
        });
    }


//    public void toSaveSubjects(List<MySubject> subject) {
//
//        Gson gson = new Gson();
//        String str_subjectJSON = gson.toJson(subject);
//        SharedPreferences sp = getSharedPreferences("SP_Data_List", Activity.MODE_PRIVATE);//创建sp对象
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString("SUBJECT_LIST", str_subjectJSON); //存入json串
//        editor.commit();//提交
//        Log.e(TAG, "toSaveSubjects: " + str_subjectJSON);
//
//    }
//
//    public List<MySubject> toGetSubjects() {
//
//        SharedPreferences sp = getSharedPreferences("SP_Data_List", Activity.MODE_PRIVATE);//创建sp对象
//        String str_subjectJSON = sp.getString("SUBJECT_LIST", null);  //取出key为"SUBJECT_LIST"的值，如果值为空，则将第二个参数作为默认值赋值
//        Log.e(TAG, "toGetSubjects: " + str_subjectJSON);//str_subjectJSON便是取出的数据了
//        Gson gson = new Gson();
//        List<MySubject> subjectList = gson.fromJson(str_subjectJSON, new TypeToken<List<MySubject>>() {
//        }.getType());
//        return subjectList;
//    }
}