from django.contrib import admin
from timetable.models import Timetable

@admin.register(Timetable)
class TimetableAdmin(admin.ModelAdmin):
    list_display = ('semester', 'day', 'subject', 'faculty', 'room_no', 'start_time', 'end_time')
    list_filter = ('semester', 'day', 'faculty')
    search_fields = ('subject', 'room_no', 'faculty__email')
