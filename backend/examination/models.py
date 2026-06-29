from django.db import models

class Examination(models.Model):
    subject = models.CharField(max_length=150)
    semester = models.IntegerField()
    exam_type = models.CharField(max_length=100) # MIDTERM, ENDSEM, PRACTICAL
    exam_date = models.DateField()
    start_time = models.TimeField()
    end_time = models.TimeField()

    def __str__(self):
        return f"{self.subject} ({self.exam_type}) - {self.exam_date}"
