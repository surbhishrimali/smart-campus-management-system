import os
from django.db import models
from django.core.exceptions import ValidationError
from accounts.models import User

def validate_pdf_extension(value):
    ext = os.path.splitext(value.name)[1]
    if not ext.lower() == '.pdf':
        raise ValidationError('Only PDF files are allowed.')

class Resource(models.Model):
    title = models.CharField(max_length=200)
    description = models.TextField()
    resource_type = models.CharField(max_length=50, null=True, blank=True) # NOTE, PYQ, BOOK, etc. (kept for legacy support)
    pdf_file = models.FileField(upload_to='resources/', null=True, blank=True, validators=[validate_pdf_extension])
    uploaded_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='uploaded_resources')
    department = models.CharField(max_length=100, null=True, blank=True) # (kept for legacy support)
    created_at = models.DateTimeField(auto_now_add=True) # (kept for legacy support)
    
    # New required fields
    subject = models.ForeignKey('academics.Subject', on_delete=models.SET_NULL, null=True, blank=True, related_name='resources')
    uploaded_at = models.DateTimeField(auto_now_add=True, null=True, blank=True)
    year = models.IntegerField(null=True, blank=True)

    def __str__(self):
        return self.title
