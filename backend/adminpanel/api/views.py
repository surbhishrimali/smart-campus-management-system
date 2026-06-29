from django.utils import timezone
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import permissions, status

from accounts.models import User
from resources.models import Resource
from notifications.models import Notification
from complaints.models import Complaint
from certificates.models import Certificate
from attendance.models import Attendance
from accounts.permissions import IsAdmin

class AdminDashboardSummaryView(APIView):
    permission_classes = [permissions.IsAuthenticated, IsAdmin]

    def get(self, request, *args, **kwargs):
        today = timezone.now().date()
        
        data = {
            "total_students": User.objects.filter(role=User.Role.STUDENT).count(),
            "total_faculty": User.objects.filter(role=User.Role.FACULTY).count(),
            "total_resources": Resource.objects.count(),
            "total_notifications": Notification.objects.count(),
            "pending_complaints": Complaint.objects.filter(status='PENDING').count(),
            "pending_certificates": Certificate.objects.filter(status='PENDING').count(),
            "attendance_today": Attendance.objects.filter(date=today).count(),
        }
        
        return Response(data, status=status.HTTP_200_OK)
