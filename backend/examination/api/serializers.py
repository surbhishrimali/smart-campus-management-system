from rest_framework import serializers
from examination.models import Examination

class ExaminationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Examination
        fields = [
            'id',
            'subject',
            'semester',
            'exam_type',
            'exam_date',
            'start_time',
            'end_time',
        ]
