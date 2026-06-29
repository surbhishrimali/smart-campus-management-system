from rest_framework import serializers
from resources.models import Resource

class ResourceSerializer(serializers.ModelSerializer):
    uploader_name = serializers.CharField(source='uploaded_by.full_name', read_only=True)

    class Meta:
        model = Resource
        fields = [
            'id', 'title', 'description', 'resource_type', 'pdf_file', 
            'uploaded_by', 'uploader_name', 'department', 'created_at',
            'subject', 'uploaded_at'
        ]
        read_only_fields = ['uploaded_by']
