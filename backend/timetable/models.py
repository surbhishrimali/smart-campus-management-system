from django.db import models
from accounts.models import User

class Timetable(models.Model):
    DAY_CHOICES = [
        ('MONDAY', 'Monday'),
        ('TUESDAY', 'Tuesday'),
        ('WEDNESDAY', 'Wednesday'),
        ('THURSDAY', 'Thursday'),
        ('FRIDAY', 'Friday'),
        ('SATURDAY', 'Saturday'),
        ('SUNDAY', 'Sunday'),
    ]
    semester = models.IntegerField()
    day = models.CharField(max_length=20, choices=DAY_CHOICES)
    subject = models.CharField(max_length=150)
    faculty = models.ForeignKey(User, on_delete=models.CASCADE, limit_choices_to={'role': User.Role.FACULTY}, related_name='timetable_classes')
    room_no = models.CharField(max_length=50)
    start_time = models.TimeField()
    end_time = models.TimeField()

    def __str__(self):
        return f"Sem {self.semester} - {self.day} - {self.subject} ({self.start_time} - {self.end_time})"
