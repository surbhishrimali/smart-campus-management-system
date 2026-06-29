from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from certificates.models import Certificate
import datetime

class CertificateAPITests(APITestCase):
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
        
        self.certificate = Certificate.objects.create(
            student=self.student_user,
            title='Degree Completion Certificate',
            status='PENDING'
        )
        
        self.list_create_url = reverse('certificate-list')
        self.detail_url = reverse('certificate-detail', kwargs={'pk': self.certificate.pk})

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_student_request_certificate(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'title': 'No Dues Certificate'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['status'], 'PENDING')

    def test_admin_approve_certificate(self):
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'status': 'APPROVED',
            'issued_by': 'Registrar Office',
            'issue_date': str(datetime.date.today())
        }
        response = self.client.patch(self.detail_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['status'], 'APPROVED')
