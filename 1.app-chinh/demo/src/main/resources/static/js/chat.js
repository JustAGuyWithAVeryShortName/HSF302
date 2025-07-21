let stompClient = null;

window.addEventListener('DOMContentLoaded', () => {
    // 1. Tải lịch sử chat
    fetch('/api/chat/history')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    console.warn("Not authorized to fetch chat history. Redirecting to login if not authenticated.");
                    if (!isAuthenticatedUser) {
                        window.location.href = "/login";
                    }
                }
                throw new Error('Failed to fetch chat history: ' + response.statusText);
            }
            return response.json();
        })
        .then(messages => {
            messages.forEach(message => {
                appendMessage(message);
            });

        })
        .catch(error => {
            console.error('Error fetching chat history:', error);
        });

    // ✅ 2. Delay 500ms rồi kết nối WebSocket (chờ cookie sau login)
    setTimeout(() => {
        connectSocket();
    }, 500);

    // 3. Gửi tin nhắn khi submit
    const chatForm = document.getElementById('chat-form');
    if (chatForm) {
        chatForm.addEventListener('submit', function (event) {
            event.preventDefault();

            if (!isAuthenticatedUser) {
                console.warn("User not authenticated. Redirecting to login.");
                window.location.href = "/login";
                return;
            }

            const content = document.getElementById('message-content').value;
            if (content.trim() !== "") {
                if (stompClient && stompClient.connected) {
                    stompClient.send("/app/chat.send", {}, JSON.stringify({ content: content }));
                    document.getElementById('message-content').value = '';
                } else {
                    console.warn("STOMP client not connected. Attempting to reconnect...");
                    connectSocket();
                }
            }
        });
    }
});


function connectSocket() {
    if (stompClient && stompClient.connected) {
        console.log("Already connected, skipping reconnect.");
        return;
    }

    const socket = new SockJS("/ws"); // ✅ Đơn giản và đúng nhất nếu cùng host
    stompClient = Stomp.over(socket);


    stompClient.connect({}, function (frame) {
        console.log('Connected to STOMP: ' + frame);

        stompClient.subscribe('/topic/public', function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            appendMessage(message);
        });
    }, function (error) {
        console.error('STOMP connection error:', error);
    });
}

function appendMessage(message) {
    const table = document.getElementById('chat-table');
    if (!table) return;

    const row = table.insertRow(-1);
    const cell = row.insertCell(0);

    const lastName = message.lastName || "";
    const firstName = message.firstName || "";
    const fullName = `${lastName} ${firstName}`.trim();

    const nameSpan = document.createElement('span');

    // --- REMOVED HARDCODED PREFIXES ---
    // Now it will only display the full name (or whatever fullName resolves to)
    nameSpan.textContent = fullName;

    // Check against the username from the DTO
    if (message.username === currentUsername) {
        nameSpan.style.color = "blue";
        nameSpan.style.fontWeight = "bold";
    }

    const messageSpan = document.createElement('span');
    messageSpan.textContent = `: ${message.content}`;

    cell.appendChild(nameSpan);
    cell.appendChild(messageSpan);

    const wrapper = document.querySelector('.chat-table-wrapper');
    if (wrapper) {
        wrapper.scrollTop = wrapper.scrollHeight;
    }
}