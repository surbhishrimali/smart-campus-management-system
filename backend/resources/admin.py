from django.contrib import admin
from resources.models import Resource

@admin.register(Resource)
class ResourceAdmin(admin.ModelAdmin):
    list_display = ('title', 'resource_type', 'subject', 'uploaded_by', 'uploaded_at')
    list_filter = ('resource_type', 'subject', 'uploaded_at')
    search_fields = ('title', 'description', 'subject', 'uploaded_by__email')

