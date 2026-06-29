from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from results.models import Result

class ResultAPITests(APITestCase):
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
        
        self.result = Result.objects.create(
            student=self.student_user,
            subject='Cryptography',
            semester=8,
            exam_type='END_SEM',
            marks_obtained=90,
            max_marks=100,
            grade='A+',
            published_by=self.faculty_user
        )
        
        self.list_create_url = reverse('result-list')
        self.detail_url = reverse('result-detail', kwargs={'pk': self.result.pk})

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_student_read_own_results(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.detail_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['grade'], 'A+')

    def test_faculty_publish_results(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'student': self.student_user.id,
            'subject': 'Network Security',
            'semester': 8,
            'exam_type': 'MID_TERM',
            'marks_obtained': 45,
            'max_marks': 50,
            'grade': 'A'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
