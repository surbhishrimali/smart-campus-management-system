from django.db import models
from accounts.models import User

class Attendance(models.Model):
    STATUS_CHOICES = [
        ('Present', 'Present'),
        ('Absent', 'Absent'),
        ('Leave', 'Leave'),
    ]
    student = models.ForeignKey(User, on_delete=models.CASCADE, limit_choices_to={'role': User.Role.STUDENT}, related_name='attendances')
    faculty = models.ForeignKey(User, on_delete=models.CASCADE, limit_choices_to={'role': User.Role.FACULTY}, related_name='marked_attendances', null=True, blank=True)
    subject = models.CharField(max_length=150, null=True, blank=True)
    date = models.DateField()
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, null=True, blank=True)
    remarks = models.TextField(blank=True, default='')
    
    # New fields for Android App integration
    student_class = models.IntegerField(null=True, blank=True)
    is_present = models.BooleanField(default=True)

    def save(self, *args, **kwargs):
        if self.status:
            status_lower = self.status.lower()
            if status_lower == 'present':
                self.status = 'Present'
                self.is_present = True
            elif status_lower == 'absent':
                self.status = 'Absent'
                self.is_present = False
            elif status_lower == 'leave':
                self.status = 'Leave'
        
        if self.is_present is not None and not self.status:
            self.status = 'Present' if self.is_present else 'Absent'
            
        super().save(*args, **kwargs)

    def __str__(self):
        return f"{self.student.email} - {self.subject or self.student_class} - {self.date}: {self.status}"
