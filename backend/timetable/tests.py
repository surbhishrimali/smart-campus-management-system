from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from timetable.models import Timetable

class TimetableAPITests(APITestCase):
    def setUp(self):
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
        
        self.timetable = Timetable.objects.create(
            semester=8,
            day='MONDAY',
            subject='Project Management',
            faculty=self.faculty_user,
            room_no='Lab 2',
            start_time='09:00:00',
            end_time='10:30:00'
        )
        
        self.list_create_url = reverse('timetable-list')

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_student_read_timetable(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['count'], 1)

    def test_admin_create_timetable(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'semester': 8,
            'day': 'TUESDAY',
            'subject': 'Compiler Lab',
            'faculty': self.faculty_user.id,
            'room_no': 'Lab 4',
            'start_time': '11:00:00',
            'end_time': '12:30:00'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
