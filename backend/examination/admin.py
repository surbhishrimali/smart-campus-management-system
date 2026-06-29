from django.contrib import admin
from examination.models import Examination

@admin.register(Examination)
class ExaminationAdmin(admin.ModelAdmin):
    list_display = ('subject', 'semester', 'exam_type', 'exam_date', 'start_time', 'end_time')
    list_filter = ('semester', 'exam_type', 'exam_date')
    search_fields = ('subject', 'exam_type')
