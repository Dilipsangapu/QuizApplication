let totalTime = 300; // 5 minutes

function startTimer() {
    const timer = document.getElementById('timer');
    const interval = setInterval(() => {
        const minutes = Math.floor(totalTime / 60);
        const seconds = totalTime % 60;
        timer.textContent = minutes + ":" + (seconds < 10 ? '0' : '') + seconds;
        totalTime--;

        if(totalTime < 0) {
            clearInterval(interval);
            alert('Time is up! Submitting quiz.');
            document.getElementById('quizForm').submit();
        }
    }, 1000);
}
