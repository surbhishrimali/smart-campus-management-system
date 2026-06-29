import datetime
import random
from django.core.management.base import BaseCommand
from accounts.models import User
from students.models import StudentProfile
from faculty.models import FacultyProfile
from resources.models import Resource
from notifications.models import Notification
from attendance.models import Attendance
from results.models import Result
from academics.models import Course, Subject
from certificates.models import Certificate
from complaints.models import Complaint

class Command(BaseCommand):
    help = 'Generates sample test data for Smart Campus Management System'

    def handle(self, *args, **kwargs):
        self.stdout.write("Cleaning existing sample data...")
        # Clear existing data to avoid duplicates
        Attendance.objects.all().delete()
        Result.objects.all().delete()
        Resource.objects.all().delete()
        Notification.objects.all().delete()
        StudentProfile.objects.all().delete()
        FacultyProfile.objects.all().delete()
        Certificate.objects.all().delete()
        Complaint.objects.all().delete()
        Subject.objects.all().delete()
        Course.objects.all().delete()
        User.objects.exclude(is_superuser=True).delete()

        self.stdout.write("Generating 5 Faculty users and profiles...")
        faculties = []
        for i in range(1, 6):
            email = f"faculty{i}@campus.edu"
            user = User.objects.create_user(
                email=email,
                password="password123",
                role=User.Role.FACULTY,
                username=f"faculty{i}",
                phone_number=f"987-654-320{i}",
                full_name=f"Faculty Member {i}",
                department="Computer Science"
            )
            FacultyProfile.objects.create(
                user=user,
                faculty_id=f"FAC00{i}",
                department="Computer Science",
                designation="Assistant Professor" if i < 4 else "Associate Professor",
                phone=f"987654320{i}",
                specialization="AI/ML" if i % 2 == 0 else "Software Engineering"
            )
            faculties.append(user)
        self.stdout.write(f"Successfully generated {len(faculties)} faculty users.")

        self.stdout.write("Generating 25 Student users and profiles...")
        students = []
        for i in range(1, 26):
            email = f"student{i}@campus.edu"
            user = User.objects.create_user(
                email=email,
                password="password123",
                role=User.Role.STUDENT,
                username=f"student{i}",
                phone_number=f"123-456-78{i:02d}",
                full_name=f"Student Name {i}",
                department="Computer Science"
            )
            StudentProfile.objects.create(
                user=user,
                enrollment_number=f"ENROLL{i:03d}",
                branch="CSE",
                semester=random.choice([1, 2, 3, 4, 5, 6, 7, 8]),
                section="A" if i % 2 == 0 else "B",
                phone=f"12345678{i:02d}",
                gpa=round(random.uniform(6.0, 9.8), 2)
            )
            students.append(user)
        self.stdout.write(f"Successfully generated {len(students)} student users.")

        self.stdout.write("Generating academics courses and subjects...")
        course = Course.objects.create(name="B.Tech Computer Science & Engineering", code="CSE")
        subject_names = ["Algorithms", "Database Systems", "Machine Learning", "Operating Systems", "Computer Networks"]
        subject_objects = []
        for s_name in subject_names:
            sub = Subject.objects.create(name=s_name, semester=4, course=course)
            subject_objects.append(sub)
        self.stdout.write("Successfully generated courses and subjects.")

        self.stdout.write("Generating sample resources...")
        for i, sub_obj in enumerate(subject_objects):
            faculty = random.choice(faculties)
            # PDF note
            Resource.objects.create(
                title=f"Lecture Notes on {sub_obj.name}",
                description=f"This resource contains comprehensive lecture slides and reading materials for {sub_obj.name}.",
                subject=sub_obj,
                resource_type="NOTE",
                uploaded_by=faculty
            )
            # PYQ
            Resource.objects.create(
                title=f"Previous Year Question Paper - {sub_obj.name}",
                description=f"End-semester question paper for {sub_obj.name} from 2024.",
                subject=sub_obj,
                resource_type="PYQ",
                year=2024,
                uploaded_by=faculty
            )
        self.stdout.write("Successfully generated resources.")

        self.stdout.write("Generating sample notifications...")
        notification_types = ["ANNOUNCEMENT", "ALERT", "EVENT"]
        titles = ["Mid-term exam schedule published", "System maintenance on Sunday", "Hackathon 2026 Registration Open"]
        messages = [
            "The mid-term exams will start from next Monday. Check timetable for details.",
            "The campus portal will be offline on Sunday from 2 AM to 6 AM for database updates.",
            "Register for the annual campus hackathon. The deadline is next Friday."
        ]
        for i in range(3):
            faculty = random.choice(faculties)
            Notification.objects.create(
                title=titles[i],
                message=messages[i],
                notification_type=notification_types[i],
                sender=faculty,
                target_role="ALL" if i != 0 else "STUDENT"
            )
        self.stdout.write("Successfully generated notifications.")

        self.stdout.write("Generating sample attendance records...")
        today = datetime.date.today()
        statuses = ["Present", "Absent", "Leave"]
        # Generate attendance for the last 5 days for all students
        attendance_count = 0
        for day_offset in range(5):
            date = today - datetime.timedelta(days=day_offset)
            for student in students:
                faculty = random.choice(faculties)
                sub_obj = random.choice(subject_objects)
                status_choice = random.choices(statuses, weights=[80, 15, 5], k=1)[0]
                
                Attendance.objects.create(
                    student=student,
                    faculty=faculty,
                    subject=sub_obj.name,
                    date=date,
                    status=status_choice,
                    is_present=(status_choice == "Present"),
                    student_class=sub_obj.id  # Matches the subject id (Int) expected by Android app
                )
                attendance_count += 1
        self.stdout.write(f"Successfully generated {attendance_count} attendance records.")

        self.stdout.write("Generating sample result records...")
        result_count = 0
        grades_map = [
            (90, "A+"),
            (80, "A"),
            (70, "B"),
            (60, "C"),
            (50, "D"),
            (0, "F")
        ]
        for student in students:
            # Generate results for 3 random subjects for each student
            student_subs = random.sample(subject_objects, 3)
            for sub_obj in student_subs:
                marks = random.randint(45, 98)
                grade = next(g for score, g in grades_map if marks >= score)
                faculty = random.choice(faculties)
                profile = student.student_profile
                
                Result.objects.create(
                    student=student,
                    subject=sub_obj.name,
                    marks=marks,
                    grade=grade,
                    gpa=round(marks / 10.0, 1),
                    remarks="Pass" if marks >= 50 else "Fail",
                    semester=profile.semester,
                    published_by=faculty
                )
                result_count += 1
        self.stdout.write(f"Successfully generated {result_count} result records.")

        self.stdout.write("Generating sample certificates...")
        certificates_count = 0
        cert_titles = ["Bonafide Certificate", "Grade Card - Sem 3", "NOC for Internship"]
        for student in students:
            for title in cert_titles:
                Certificate.objects.create(
                    student=student,
                    title=title,
                    issued_by="Administration Office",
                    issue_date=today - datetime.timedelta(days=random.randint(10, 50)),
                    status="APPROVED" if random.random() > 0.3 else "PENDING"
                )
                certificates_count += 1
        self.stdout.write(f"Successfully generated {certificates_count} certificate requests.")

        self.stdout.write("Generating sample complaints...")
        complaints_count = 0
        complaint_titles = ["Wifi not working in Hostel Block A", "Library water cooler repair", "Lab computer mouse missing"]
        for student in students[:5]:
            for title in complaint_titles:
                Complaint.objects.create(
                    user=student,
                    title=title,
                    description=f"This is a detailed description of the complaint: {title}.",
                    status=random.choice(["pending", "resolved"]),
                    priority=random.choice(["LOW", "MEDIUM", "HIGH"])
                )
                complaints_count += 1
        self.stdout.write(f"Successfully generated {complaints_count} complaints.")

        self.stdout.write(self.style.SUCCESS("All sample data seeded successfully!"))
