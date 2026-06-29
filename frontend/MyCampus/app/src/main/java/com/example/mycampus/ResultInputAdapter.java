package com.example.mycampus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycampus.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ResultInputAdapter extends RecyclerView.Adapter<ResultInputAdapter.ViewHolder> {

    private List<User> students;
    private OnResultUploadListener listener;

    public interface OnResultUploadListener {
        void onUpload(User student, double marks, String grade, String remarks);
    }

    public ResultInputAdapter(List<User> students, OnResultUploadListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_result_input, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User student = students.get(position);
        holder.tvName.setText(student.username);
        holder.tvId.setText("ID: " + student.id);

        holder.btnUpload.setOnClickListener(v -> {
            String marksStr = holder.etMarks.getText().toString().trim();
            String grade = holder.etGrade.getText().toString().trim();
            String remarks = holder.etRemarks.getText().toString().trim();

            if (marksStr.isEmpty() || grade.isEmpty()) {
                return;
            }

            listener.onUpload(student, Double.parseDouble(marksStr), grade, remarks);
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;
        TextInputEditText etMarks, etGrade, etRemarks;
        Button btnUpload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvId = itemView.findViewById(R.id.tvStudentId);
            etMarks = itemView.findViewById(R.id.etMarks);
            etGrade = itemView.findViewById(R.id.etGrade);
            etRemarks = itemView.findViewById(R.id.etRemarks);
            btnUpload = itemView.findViewById(R.id.btnUploadSingleResult);
        }
    }
}
