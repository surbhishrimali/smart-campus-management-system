package com.example.mycampus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<User> students;
    private Map<Integer, String> attendanceMap = new HashMap<>(); // studentId -> status

    public AttendanceAdapter(List<User> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User student = students.get(position);
        holder.tvName.setText(student.username);
        holder.tvId.setText("ID: " + student.id);

        holder.rgStatus.setOnCheckedChangeListener(null);
        String currentStatus = attendanceMap.get(student.id);
        if (currentStatus != null) {
            if (currentStatus.equals("P")) holder.rgStatus.check(R.id.rbPresent);
            else if (currentStatus.equals("A")) holder.rgStatus.check(R.id.rbAbsent);
            else if (currentStatus.equals("L")) holder.rgStatus.check(R.id.rbLeave);
        } else {
            holder.rgStatus.clearCheck();
        }

        holder.rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPresent) attendanceMap.put(student.id, "P");
            else if (checkedId == R.id.rbAbsent) attendanceMap.put(student.id, "A");
            else if (checkedId == R.id.rbLeave) attendanceMap.put(student.id, "L");
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public Map<Integer, String> getAttendanceMap() {
        return attendanceMap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;
        RadioGroup rgStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvId = itemView.findViewById(R.id.tvStudentId);
            rgStatus = itemView.findViewById(R.id.rgStatus);
        }
    }
}
