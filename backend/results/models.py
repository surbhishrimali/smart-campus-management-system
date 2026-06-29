from django.db import models
from accounts.models import User

class Result(models.Model):
    student = models.ForeignKey(User, on_delete=models.CASCADE, limit_choices_to={'role': User.Role.STUDENT}, related_name='results')
    subject = models.CharField(max_length=150, null=True, blank=True)
    semester = models.IntegerField()
    exam_type = models.CharField(max_length=100, null=True, blank=True) # (kept for legacy support)
    marks_obtained = models.IntegerField(null=True, blank=True) # (kept for legacy support)
    max_marks = models.IntegerField(null=True, blank=True) # (kept for legacy support)
    grade = models.CharField(max_length=10, null=True, blank=True)
    result_pdf = models.FileField(upload_to='results/', null=True, blank=True) # (kept for legacy support)
    
    # New fields for Android App integration
    gpa = models.FloatField(null=True, blank=True)
    has_backlog = models.BooleanField(default=False)
    backlog_subjects = models.TextField(null=True, blank=True)
    
    # Required spec fields
    marks = models.IntegerField(null=True, blank=True)
    remarks = models.TextField(blank=True, default='')
    created_at = models.DateTimeField(auto_now_add=True, null=True, blank=True)
    
    published_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, limit_choices_to={'role': User.Role.FACULTY}, related_name='published_results')
    published_at = models.DateTimeField(auto_now_add=True)

    def save(self, *args, **kwargs):
        if self.marks is not None and self.marks_obtained is None:
            self.marks_obtained = self.marks
        elif self.marks_obtained is not None and self.marks is None:
            self.marks = self.marks_obtained
        super().save(*args, **kwargs)

    def __str__(self):
        return f"{self.student.email} - {self.subject}: {self.marks_obtained}/{self.max_marks}"
