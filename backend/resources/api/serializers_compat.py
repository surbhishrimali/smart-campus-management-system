from rest_framework import serializers
from resources.models import Resource
from academics.models import Subject

class NoteSerializer(serializers.ModelSerializer):
    file_url = serializers.SerializerMethodField()
    subject = serializers.PrimaryKeyRelatedField(queryset=Subject.objects.all(), required=False, allow_null=True)

    class Meta:
        model = Resource
        fields = ['id', 'subject', 'title', 'file_url']
        read_only_fields = ['id', 'file_url']

    def get_file_url(self, obj):
        if obj.pdf_file:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.pdf_file.url)
            return obj.pdf_file.url
        return None

    def create(self, validated_data):
        request = self.context.get('request')
        if request and 'file' in request.FILES:
            validated_data['pdf_file'] = request.FILES['file']
        return super().create(validated_data)

class PyqSerializer(serializers.ModelSerializer):
    file_url = serializers.SerializerMethodField()
    subject = serializers.PrimaryKeyRelatedField(queryset=Subject.objects.all(), required=False, allow_null=True)
    year = serializers.IntegerField(required=False, allow_null=True)

    class Meta:
        model = Resource
        fields = ['id', 'subject', 'year', 'file_url']
        read_only_fields = ['id', 'file_url']

    def get_file_url(self, obj):
        if obj.pdf_file:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.pdf_file.url)
            return obj.pdf_file.url
        return None

class YoutubeRecommendationSerializer(serializers.ModelSerializer):
    video_url = serializers.SerializerMethodField()
    subject = serializers.PrimaryKeyRelatedField(queryset=Subject.objects.all(), required=False, allow_null=True)

    class Meta:
        model = Resource
        fields = ['id', 'subject', 'title', 'video_url']
        read_only_fields = ['id', 'video_url']

    def get_video_url(self, obj):
        return obj.youtube_url or obj.youtube_link or ''
