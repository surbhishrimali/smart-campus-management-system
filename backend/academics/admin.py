from django.contrib import admin
from academics.models import Course, Subject

@admin.register(Course)
class CourseAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'code')
    search_fields = ('name', 'code')

@admin.register(Subject)
class SubjectAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'semester', 'course')
    list_filter = ('semester', 'course')
    search_fields = ('name',)
