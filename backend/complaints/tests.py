from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from complaints.models import Complaint

class ComplaintAPITests(APITestCase):
    def setUp(self):
        self.admin_user = User.objects.create_superuser(
            email='admin@campus.edu',
            password='password123',
            full_name='Admin User'
        )
        self.student_user = User.objects.create_user(
            email='student@campus.edu',
            password='password123',
            role=User.Role.STUDENT,
            full_name='Student User'
        )
        
        self.complaint = Complaint.objects.create(
            user=self.student_user,
            title='WiFi Not Working',
            description='The hostel Wi-Fi connection is down since yesterday.',
            priority='HIGH'
        )
        
        self.list_create_url = reverse('complaint-list')
        self.detail_url = reverse('complaint-detail', kwargs={'pk': self.complaint.pk})

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_student_file_complaint(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'title': 'Library Fan Faulty',
            'description': 'Fan number 3 makes a loud squeaking sound.',
            'priority': 'LOW'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    def test_admin_can_resolve_complaint(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'status': 'RESOLVED',
            'admin_reply': 'WiFi router has been replaced and testing shows good connection.'
        }
        response = self.client.patch(self.detail_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['status'], 'RESOLVED')
        self.assertEqual(response.data['resolved_by_name'], 'Admin User')
