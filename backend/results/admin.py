from django.contrib import admin
from results.models import Result

@admin.register(Result)
class ResultAdmin(admin.ModelAdmin):
    list_display = ('student', 'subject', 'semester', 'marks_obtained', 'grade', 'gpa', 'has_backlog')
    list_filter = ('semester', 'grade', 'has_backlog', 'subject')
    search_fields = ('student__email', 'student__full_name', 'subject')

