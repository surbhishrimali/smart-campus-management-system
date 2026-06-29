from django.urls import reverse
from django.utils import timezone
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from resources.models import Resource
from notifications.models import Notification
from complaints.models import Complaint
from certificates.models import Certificate
from attendance.models import Attendance

class AdminDashboardSummaryTests(APITestCase):
    def setUp(self):
        # Create users of each role
        self.admin_user = User.objects.create_superuser(
            email='admin@campus.edu',
            password='password123',
            full_name='Admin User'
        )
        self.faculty_user = User.objects.create_user(
            email='faculty@campus.edu',
            password='password123',
            role=User.Role.FACULTY,
            full_name='Faculty User'
        )
        self.student_user = User.objects.create_user(
            email='student@campus.edu',
            password='password123',
            role=User.Role.STUDENT,
            full_name='Student User'
        )

        # Create sample data for stats
        Resource.objects.create(
            title='Calculus Textbook',
            description='Math 101 Book',
            resource_type='BOOK',
            uploaded_by=self.faculty_user,
            department='Mathematics'
        )
        Notification.objects.create(
            title='Holiday Notice',
            content='Campus closed tomorrow.',
            sender=self.admin_user,
            target_role='ALL'
        )
        Complaint.objects.create(
            user=self.student_user,
            title='AC not working',
            description='Lab AC is down.',
            status='PENDING',
            priority='HIGH'
        )
        Certificate.objects.create(
            student=self.student_user,
            title='Degree Certificate',
            status='PENDING'
        )
        Attendance.objects.create(
            student=self.student_user,
            faculty=self.faculty_user,
            subject='Math 101',
            date=timezone.now().date(),
            status='PRESENT'
        )

        self.summary_url = reverse('admin-summary')

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_unauthenticated_denied(self):
        response = self.client.get(self.summary_url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_student_denied(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.summary_url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_faculty_denied(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.summary_url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_allowed_and_correct_stats(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.summary_url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        expected_data = {
            "total_students": 1,
            "total_faculty": 1,
            "total_resources": 1,
            "total_notifications": 1,
            "pending_complaints": 1,
            "pending_certificates": 1,
            "attendance_today": 1,
        }
        
        self.assertEqual(response.data, expected_data)
