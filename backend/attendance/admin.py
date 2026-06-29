from django.contrib import admin
from attendance.models import Attendance

@admin.register(Attendance)
class AttendanceAdmin(admin.ModelAdmin):
    list_display = ('student', 'faculty', 'subject', 'date', 'status', 'is_present')
    list_filter = ('status', 'is_present', 'date', 'subject')
    search_fields = ('student__email', 'student__full_name', 'subject')

