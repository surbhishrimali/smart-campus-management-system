from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from attendance.models import Attendance
import datetime

class AttendanceAPITests(APITestCase):
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
        self.another_student = User.objects.create_user(
            email='another@campus.edu',
            password='password123',
            role=User.Role.STUDENT,
            full_name='Another Student'
        )
        
        self.record = Attendance.objects.create(
            student=self.student_user,
            faculty=self.faculty_user,
            subject='Algorithms',
            date=datetime.date.today(),
            status='PRESENT',
            remarks='Attended full lecture'
        )
        
        self.list_create_url = reverse('attendance-list')
        self.detail_url = reverse('attendance-detail', kwargs={'pk': self.record.pk})

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_faculty_can_mark_attendance(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'student': self.another_student.id,
            'subject': 'Operating Systems',
            'date': str(datetime.date.today()),
            'status': 'PRESENT',
            'remarks': ''
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

    def test_student_cannot_mark_attendance(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'student': self.another_student.id,
            'subject': 'Operating Systems',
            'date': str(datetime.date.today()),
            'status': 'PRESENT'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_student_only_sees_own_records(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['count'], 1)
