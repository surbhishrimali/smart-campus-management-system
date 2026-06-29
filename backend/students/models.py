from django.db import models
from accounts.models import User

class StudentProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='student_profile')
    enrollment_number = models.CharField(max_length=50, unique=True)
    branch = models.CharField(max_length=100)
    semester = models.IntegerField(default=1)
    section = models.CharField(max_length=10)
    phone = models.CharField(max_length=20)
    gpa = models.DecimalField(max_digits=4, decimal_places=2, default=0.00)

    def __str__(self):
        return f"{self.user.email} - {self.enrollment_number}"
