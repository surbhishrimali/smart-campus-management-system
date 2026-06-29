from django.db import models
from accounts.models import User

class Certificate(models.Model):
    STATUS_CHOICES = [
        ('PENDING', 'Pending'),
        ('APPROVED', 'Approved'),
        ('REJECTED', 'Rejected'),
    ]
    student = models.ForeignKey(User, on_delete=models.CASCADE, limit_choices_to={'role': User.Role.STUDENT}, related_name='certificates')
    title = models.CharField(max_length=200)
    issued_by = models.CharField(max_length=200, blank=True, default='')
    issue_date = models.DateField(null=True, blank=True)
    certificate_file = models.FileField(upload_to='certificates/', null=True, blank=True)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')

    def __str__(self):
        return f"{self.title} ({self.status})"
