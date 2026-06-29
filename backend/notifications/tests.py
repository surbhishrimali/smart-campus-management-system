from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from notifications.models import Notification

class NotificationAPITests(APITestCase):
    def setUp(self):
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
        
        self.notification = Notification.objects.create(
            title='End-sem Exam Postponed',
            content='End-sem examinations are postponed by one week.',
            sender=self.faculty_user,
            target_role='STUDENT',
            is_active=True
        )
        
        self.list_create_url = reverse('notification-list')

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_student_can_read_notification(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['count'], 1)

    def test_faculty_can_create_notification(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'title': 'Faculty Meeting',
            'content': 'Meeting in the conference hall.',
            'target_role': 'FACULTY',
            'is_active': True
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
