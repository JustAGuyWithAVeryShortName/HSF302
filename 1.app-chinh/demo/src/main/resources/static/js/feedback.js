document.addEventListener('DOMContentLoaded', function() {
    const feedbackForm = document.getElementById('feedback-form');
    const feedbackMessageDiv = document.getElementById('feedbackMessage');
    const feedbackComment = document.getElementById('feedback-comment');
    const ratingInputs = document.querySelectorAll('input[name="rating"]');

    // Get CSRF token for Spring Security
    const csrfToken = document.querySelector("meta[name='_csrf']") ? document.querySelector("meta[name='_csrf']").content : '';
    const csrfHeader = document.querySelector("meta[name='_csrf_header']") ? document.querySelector("meta[name='_csrf_header']").content : '';

    if (feedbackForm) {
        feedbackForm.addEventListener('submit', function(event) {
            event.preventDefault(); // Prevent default form submission

            const selectedRating = document.querySelector('input[name="rating"]:checked');
            const rating = selectedRating ? parseInt(selectedRating.value) : 0; // Default to 0 if no star is selected
            const comment = feedbackComment.value.trim();

            if (rating === 0 && comment === "") {
                feedbackMessageDiv.textContent = 'Please provide a rating or a comment.';
                feedbackMessageDiv.style.color = 'orange';
                return;
            }

            feedbackMessageDiv.textContent = 'Submitting feedback...';
            feedbackMessageDiv.style.color = 'gray';

            const feedbackData = {
                rating: rating,
                comment: comment
            };

            fetch('/api/feedback', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken // Include CSRF token for security
                },
                body: JSON.stringify(feedbackData)
            })
                .then(response => {
                    if (!response.ok) {
                        // Check if response is JSON
                        return response.json().then(errorData => {
                            throw new Error(errorData.message || 'Failed to submit feedback.');
                        }).catch(() => {
                            // If not JSON, throw generic error
                            throw new Error('Failed to submit feedback (non-JSON response).');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    feedbackMessageDiv.textContent = 'Thank you for your feedback!';
                    feedbackMessageDiv.style.color = 'green';
                    feedbackComment.value = ''; // Clear comment field
                    ratingInputs.forEach(input => input.checked = false); // Uncheck stars
                })
                .catch(error => {
                    console.error('Error submitting feedback:', error);
                    feedbackMessageDiv.textContent = 'Error: ' + error.message;
                    feedbackMessageDiv.style.color = 'red';
                });
        });
    }

    // Optional: Fetch and display existing feedback if needed (e.g., average rating)
    // You would need another API endpoint for this.
});