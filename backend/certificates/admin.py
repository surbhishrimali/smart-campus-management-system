from django.contrib import admin
from certificates.models import Certificate

@admin.register(Certificate)
class CertificateAdmin(admin.ModelAdmin):
    list_display = ('title', 'student', 'issued_by', 'issue_date', 'status')
    list_filter = ('status', 'issue_date')
    search_fields = ('title', 'student__email', 'issued_by')

