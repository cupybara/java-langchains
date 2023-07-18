package io.github.cupybara.javalangchains.chains.data.retrieval;

import java.util.Arrays;
import java.util.List;

public class DocumentTestUtil {
    // @formatter:off
    public static final String DOCUMENT_1 =
            "Subject: John Doe's Biography\n"
                    + "Dear Reader,\n"
                    + "I am delighted to present to you the biography of John Doe, a remarkable individual who has left an indelible mark on society. Born and raised in a small town, John displayed an insatiable curiosity and a thirst for knowledge from a young age. He excelled academically, earning scholarships that allowed him to attend prestigious universities and pursue his passion for scientific research.\n"
                    + "Throughout his career, John made groundbreaking discoveries in the field of medicine, revolutionizing treatment options for previously incurable diseases. His relentless dedication and tireless efforts have saved countless lives and earned him numerous accolades, including the Nobel Prize in Medicine.\n"
                    + "However, John's impact extends far beyond his professional accomplishments. He is known for his philanthropic endeavors, establishing charitable foundations that provide support and opportunities to underprivileged communities. John's compassion and commitment to social justice have inspired many to follow in his footsteps.\n"
                    + "In his personal life, John is a devoted family man. He cherishes the time spent with his loving wife and children, always prioritizing their well-being amidst his demanding schedule. Despite his remarkable success, John remains humble and grounded, never forgetting his roots and always seeking ways to uplift those around him.\n"
                    + "In conclusion, John Doe is not only a brilliant scientist and humanitarian but also a role model for future generations. His unwavering determination, kindness, and pursuit of excellence make him a true legend.\n"
                    + "Sincerely,\n"
                    + "Jane Doe";

    public static final String DOCUMENT_2 =
            "Subject: Invitation to John Doe's Art Exhibition\n"
                    + "Dear Art Enthusiast,\n"
                    + "We are pleased to invite you to a remarkable art exhibition featuring the mesmerizing works of John Doe. Renowned for his unique style and ability to capture the essence of emotions on canvas, John has curated a collection that will leave you awe-struck.\n"
                    + "Drawing inspiration from his diverse life experiences, John's art tells compelling stories and invites viewers to delve into the depths of their imagination. Each stroke of the brush reveals a glimpse into his creative mind, conveying a range of emotions that resonate with the observer.\n"
                    + "The exhibition will be held at the prestigious XYZ Art Gallery on [date] at [time]. It promises to be an evening filled with artistic brilliance, where you will have the opportunity to meet John Doe in person and gain insights into his creative process. Light refreshments will be served, providing a delightful ambiance for engaging discussions with fellow art enthusiasts.\n"
                    + "Kindly RSVP by [RSVP date] to ensure your attendance at this exclusive event. We look forward to your presence and sharing this unforgettable artistic journey with you.\n"
                    + "Yours sincerely,\n"
                    + "Jane Doe";

    public static final String DOCUMENT_3 =
            "Subject: John Doe's Travel Memoir - Exploring the Unknown\n"
                    + "Dear Adventurers,\n"
                    + "Prepare to embark on an extraordinary journey as we delve into the captivating travel memoir of John Doe. Throughout his life, John has traversed the globe, seeking out the hidden gems and immersing himself in diverse cultures. His memoir is a testament to the transformative power of travel and the profound impact it can have on one's perspective.\n"
                    + "From the bustling streets of Tokyo to the serene beaches of Bali, John's vivid descriptions transport readers to each destination, allowing them to experience the sights, sounds, and flavors firsthand. With a keen eye for detail and a genuine curiosity for the world, he uncovers the untold stories that lie beneath the surface, providing a fresh and unique perspective.\n"
                    + "Through his encounters with locals, John unearths the beauty of human connection and the universal language of kindness. He shares anecdotes that will make you laugh, moments that will leave you in awe, and reflections that will inspire you to embark on your own adventures.\n"
                    + "This travel memoir not only serves as a guide to off-the-beaten-path destinations but also as a reminder of the inherent beauty and diversity of our planet. It encourages readers to step out of their comfort zones, embrace new experiences, and celebrate the richness of different cultures.\n"
                    + "Whether you are an avid traveler or an armchair explorer, John Doe's memoir is a captivating read that will ignite your wanderlust and leave you yearning for new horizons. Join him on this literary expedition and discover the world through his eyes.\n"
                    + "Happy reading,\n"
                    + "Jane Doe";

    public static final List<String> DOCUMENTS = Arrays.asList(DocumentTestUtil.DOCUMENT_1, DocumentTestUtil.DOCUMENT_2, DocumentTestUtil.DOCUMENT_3);
    // @formatter:on

    private DocumentTestUtil() {

    }
}
