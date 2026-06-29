from django.contrib import admin
from students.models import StudentProfile

@admin.register(StudentProfile)
class StudentProfileAdmin(admin.ModelAdmin):
    list_display = ('user', 'enrollment_number', 'branch', 'semester', 'section', 'gpa')
    list_filter = ('branch', 'semester', 'section')
    search_fields = ('user__email', 'enrollment_number', 'user__full_name')

