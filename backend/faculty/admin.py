from django.contrib import admin
from faculty.models import FacultyProfile

@admin.register(FacultyProfile)
class FacultyProfileAdmin(admin.ModelAdmin):
    list_display = ('user', 'faculty_id', 'department', 'designation', 'phone', 'specialization')
    list_filter = ('department', 'designation')
    search_fields = ('user__email', 'faculty_id', 'user__full_name')

