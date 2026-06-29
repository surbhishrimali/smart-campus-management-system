from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from examination.models import Examination

class ExaminationAPITests(APITestCase):
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
        
        self.exam = Examination.objects.create(
            subject='Advanced Algorithms',
            semester=8,
            exam_type='ENDSEM',
            exam_date='2026-06-30',
            start_time='10:00:00',
            end_time='13:00:00'
        )
        
        self.list_create_url = reverse('examination-list')

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_unauthenticated_cannot_read(self):
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_student_read_examinations(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['count'], 1)

    def test_faculty_read_examinations(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.list_create_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_student_cannot_create_examination(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'subject': 'Database Systems',
            'semester': 4,
            'exam_type': 'MIDTERM',
            'exam_date': '2026-07-05',
            'start_time': '09:00:00',
            'end_time': '11:00:00'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_create_examination(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'subject': 'Database Systems',
            'semester': 4,
            'exam_type': 'MIDTERM',
            'exam_date': '2026-07-05',
            'start_time': '09:00:00',
            'end_time': '11:00:00'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(Examination.objects.count(), 2)

    def test_admin_delete_examination(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        detail_url = reverse('examination-detail', kwargs={'pk': self.exam.pk})
        response = self.client.delete(detail_url)
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertEqual(Examination.objects.count(), 0)
