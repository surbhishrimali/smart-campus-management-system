from django.db import models
from accounts.models import User

class FacultyProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='faculty_profile')
    faculty_id = models.CharField(max_length=50, unique=True)
    department = models.CharField(max_length=100)
    designation = models.CharField(max_length=100)
    phone = models.CharField(max_length=20)
    specialization = models.CharField(max_length=200)

    def __str__(self):
        return f"{self.user.email} - {self.faculty_id}"
